package it.paybay.quigroup.loyalty.jbpm;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:META-INF/spring/redis.xml"})
public class RedisTemplateIT {
	
	@Autowired private JedisConnectionFactory connectionFactory;
	
	// inject the actual template
    @Autowired private RedisTemplate<String, String> redisTemplate;

    // inject the template as ListOperations
    // can also inject as Value, Set, ZSet, and HashOperations
    @Resource(name="redisTemplate") private ListOperations<String, String> listOps;
	
    @Test
	public void redisConnectionTest() {
		
		JedisConnection connection = connectionFactory.getConnection();
		String pong = connection.ping();
		System.out.println( "RESPONSE IS :" + pong );
		
		Long first = listOps.leftPush("dummy_uid1", "http://dummy/url");
		System.out.println("FIRST LONG IS : " + first);
		
        // or use template directly
        Long second = redisTemplate.boundListOps("dummy_uid2").leftPush("http://dummy/url");
        System.out.println("SECOND LONG IS : " + second);
	}
}