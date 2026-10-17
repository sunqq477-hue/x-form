package cn.xiaofuge.x.infrastructure.adapter.port;

import cn.xiaofuge.x.domain.form.adapter.port.IFormDefinitionCache;
import cn.xiaofuge.x.domain.form.model.FormDefinition;
import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class FormDefinitionRedisCache implements IFormDefinitionCache {
    private static final Logger LOGGER = Logger.getLogger(FormDefinitionRedisCache.class.getName());
    private static final String KEY_PREFIX = "x-form:definition:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public FormDefinitionRedisCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public FormDefinition get(String shareCode) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + shareCode);
            return json == null ? null : JSON.parseObject(json, FormDefinition.class);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Redis 读取表单缓存失败，回退到 MySQL", exception);
            return null;
        }
    }

    @Override
    public void put(FormDefinition form) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + form.getShareCode(), JSON.toJSONString(form), TTL);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Redis 写入表单缓存失败，继续使用 MySQL", exception);
        }
    }

    @Override
    public void evict(String shareCode) {
        try {
            redisTemplate.delete(KEY_PREFIX + shareCode);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Redis 清除表单缓存失败", exception);
        }
    }
}
