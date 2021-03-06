/**
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.shedlock.provider.redis.spring;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.test.support.AbstractLockProviderIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractRedisLockProviderIntegrationTest extends AbstractLockProviderIntegrationTest {
    private static RedisServer redisServer;
    private RedisLockProvider lockProvider;
    private StringRedisTemplate redisTemplate;

    protected final static int PORT = 6380;
    protected final static String HOST = "localhost";
    private final static String ENV = "test";
    private final static String KEY_PREFIX = "test-prefix";


    @BeforeAll
    public static void startRedis() throws IOException {
        redisServer = new RedisServer(PORT);
        redisServer.start();
    }

    @AfterAll
    public static void stopRedis() {
        redisServer.stop();
    }


    public AbstractRedisLockProviderIntegrationTest(RedisConnectionFactory connectionFactory) {
        lockProvider = new RedisLockProvider.Builder(connectionFactory)
            .environment(ENV)
            .keyPrefix(KEY_PREFIX)
            .build();

        redisTemplate = new StringRedisTemplate(connectionFactory);
    }

    @Override
    protected LockProvider getLockProvider() {
        return lockProvider;
    }

    @Override
    protected void assertUnlocked(String lockName) {
        assertThat(redisTemplate.hasKey(buildKey(lockName))).isFalse();
    }

    private String buildKey(String lockName) {
        return lockProvider.buildKey(lockName);
    }

    @Override
    protected void assertLocked(String lockName) {
        assertThat(redisTemplate.getExpire(buildKey(lockName))).isGreaterThan(0);
    }
}
