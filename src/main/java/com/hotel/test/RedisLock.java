package com.hotel.test;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.*;

/**
 * 
 * 
 * @Title: RedisLock.java
 * @Package com.bc.jiangbei.o2o.portal.support.kit
 * @Description: Redis分布式锁
 * @author cy.wang
 * @date 2018年9月19日 下午1:05:58
 * @version V1.0
 *
 */
public class RedisLock {

	private static Logger LOGGER = LoggerFactory.getLogger(RedisLock.class);

	private RedisTemplate<String, Object> redisTemplate;

	/**
	 * 将key 的值设为value ，当且仅当key 不存在，等效于 SETNX。
	 */
	private static final String NX = "NX";

	/**
	 * seconds — 以秒为单位设置 key 的过期时间，等效于EXPIRE key seconds
	 */
	private static final String EX = "EX";

	/**
	 * 调用set后的返回值
	 */
	private static final String OK = "OK";

	/**
	 * 默认请求锁的超时时间(ms 毫秒)
	 */
	private static final long TIME_OUT = 100;

	/**
	 * 默认锁的有效时间(s)
	 */
	private static final int EXPIRE = 60;

	/**
	 * 解锁的lua脚本
	 */
	private String UNLOCK_LUA;

	/**
	 * 锁标志对应的key
	 */
	private String lockKey;

	/**
	 * 记录到日志的锁标志对应的key
	 */
	private String lockKeyLog = "";

	/**
	 * 锁对应的值
	 */
	private String lockValue;

	/**
	 * 锁的有效时间(s)
	 */
	private int expireTime = EXPIRE;

	/**
	 * 请求锁的超时时间(ms)
	 */
	private long timeOut = TIME_OUT;

	/**
	 * 锁标记
	 */
	private volatile boolean locked = false;

	private final Random random = new Random();

	/**
	 * 使用默认的锁过期时间和请求锁的超时时间
	 *
	 * @param redisTemplate
	 * @param lockKey
	 *            锁的key（Redis的Key）
	 */
	public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey) {
		this.redisTemplate = redisTemplate;
		this.lockKey = lockKey + "_lock";
		buildScript();
	}

	/**
	 * 使用默认的请求锁的超时时间，指定锁的过期时间
	 *
	 * @param redisTemplate
	 * @param lockKey
	 *            锁的key（Redis的Key）
	 * @param expireTime
	 *            锁的过期时间(单位：秒)
	 */
	public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireTime) {
		this(redisTemplate, lockKey);
		this.expireTime = expireTime;
	}

	/**
	 * 使用默认的锁的过期时间，指定请求锁的超时时间
	 *
	 * @param redisTemplate
	 * @param lockKey
	 *            锁的key（Redis的Key）
	 * @param timeOut
	 *            请求锁的超时时间(单位：毫秒)
	 */
	public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, long timeOut) {
		this(redisTemplate, lockKey);
		this.timeOut = timeOut;
	}

	/**
	 * 锁的过期时间和请求锁的超时时间都是用指定的值
	 *
	 * @param redisTemplate
	 * @param lockKey
	 *            锁的key（Redis的Key）
	 * @param expireTime
	 *            锁的过期时间(单位：秒)
	 * @param timeOut
	 *            请求锁的超时时间(单位：毫秒)
	 */
	public RedisLock(RedisTemplate<String, Object> redisTemplate, String lockKey, int expireTime, long timeOut) {
		this(redisTemplate, lockKey, expireTime);
		this.timeOut = timeOut;
	}

	/**
	 * (请求锁超时时间内自旋)尝试获取锁 超时返回
	 *
	 * @return
	 */
	public boolean tryLock() {
		// 生成随机key
		lockValue = UUID.randomUUID().toString();
		// 请求锁超时时间，纳秒
		long timeout = timeOut * 1000000;
		// 系统当前时间，纳秒
		long nowTime = System.nanoTime();
		while ((System.nanoTime() - nowTime) < timeout) {
			if (OK.equalsIgnoreCase(this.set(lockKey, lockValue, expireTime))) {
				locked = true;
				// 上锁成功结束请求
				return locked;
			}
			// 每次请求等待一段时间
			seleep(10, 50000);
		}
		return locked;
	}

	/**
	 * 尝试获取锁 立即返回
	 *
	 * @return 是否成功获得锁
	 */
	public boolean lock() {
		lockValue = UUID.randomUUID().toString();
		// 不存在则添加 且设置过期时间（单位ms）
		String result = set(lockKey, lockValue, expireTime);
		locked = OK.equalsIgnoreCase(result);
		return locked;
	}

	/**
	 * 以阻塞方式的获取锁
	 *
	 * @return 是否成功获得锁
	 */
	public boolean lockBlock() {
		lockValue = UUID.randomUUID().toString();
		while (true) {
			// 不存在则添加 且设置过期时间（单位ms）
			String result = set(lockKey, lockValue, expireTime);
			if (OK.equalsIgnoreCase(result)) {
				locked = true;
				return locked;
			}
			// 每次请求等待一段时间
			seleep(10, 50000);
		}
	}

	/**
	 * 解锁 不使用固定的字符串作为键的值，而是设置一个不可猜测的长随机字符串，作为口令串（token）。 不使用 DEL 命令来释放锁，而是发送一个
	 * Lua 脚本，这个脚本只在客户端传入的值和键的口令串相匹配时，才对键进行删除。 这两个改动可以防止持有过期锁的客户端误删现有锁的情况出现。
	 */
	public Boolean unlock() {
		// 只有加锁成功并且锁还有效才去释放锁
		if (locked) {
			return redisTemplate.execute(new RedisCallback<Boolean>() {
				@Override
				public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
					Object nativeConnection = connection.getNativeConnection();
					Long result = 0L;

					List<String> keys = new ArrayList<>();
					keys.add(lockKey);
					List<String> values = new ArrayList<>();
					values.add(lockValue);

					// 集群模式
					if (nativeConnection instanceof JedisCluster) {
						result = (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, keys, values);
					}

					// 单机模式
					if (nativeConnection instanceof Jedis) {
						result = (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, keys, values);
					}

					if (result == 0 && !StringUtils.isEmpty(lockKeyLog)) {
						LOGGER.info("Redis分布式锁，【{}】解锁失败！解锁时间：【{}】", lockKeyLog,
								DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
					}
					locked = result == 0;
					return result == 1;
				}
			});
		}
		return true;
	}

	/**
	 * 获取锁状态
	 * 
	 * @Title: isLock
	 * @Description:
	 * @return
	 */
	public boolean isLock() {
		return locked;
	}

	/**
	 * 重写redisTemplate的set方法
	 * <p>
	 * 命令 SET resource-name anystring NX EX max-lock-time 是一种在 Redis 中实现锁的简单方法。
	 * <p>
	 * 客户端执行以上的命令：
	 * <p>
	 * 如果服务器返回 OK ，那么这个客户端获得锁。 如果服务器返回 NIL ，那么客户端获取锁失败，可以在稍后再重试。
	 *
	 * @param key
	 *            锁的Key
	 * @param value
	 *            锁里面的值
	 * @param seconds
	 *            过去时间（秒）
	 * @return
	 */
	private String set(final String key, final String value, final long seconds) {
		Assert.isTrue(!StringUtils.isEmpty(key), "key不能为空");
		return redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				Object nativeConnection = connection.getNativeConnection();
				String result = null;
				if (nativeConnection instanceof JedisCommands) {
					result = ((JedisCommands) nativeConnection).set(key, value, NX, EX, seconds);
				}

				if (!StringUtils.isEmpty(lockKeyLog) && !StringUtils.isEmpty(result)) {
					LOGGER.info("获取锁{}的时间：{}", lockKeyLog, System.currentTimeMillis());
				}
				return result;
			}
		});
	}

	/**
	 * @param millis
	 *            毫秒
	 * @param nanos
	 *            纳秒
	 * @Title: seleep
	 * @Description: 线程等待时间
	 */
	private void seleep(long millis, int nanos) {
		try {
			Thread.sleep(millis, random.nextInt(nanos));
		} catch (InterruptedException e) {
			LOGGER.info("获取分布式锁休眠被中断：", e);
		}
	}

	private void buildScript() {
		UNLOCK_LUA = ScriptUtil.getScript("lock.lua");
	}

	public String getLockKeyLog() {
		return lockKeyLog;
	}

	public void setLockKeyLog(String lockKeyLog) {
		this.lockKeyLog = lockKeyLog;
	}

	public int getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(int expireTime) {
		this.expireTime = expireTime;
	}

	public long getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}
}
