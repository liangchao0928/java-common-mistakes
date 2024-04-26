package javaprogramming.commonmistakes.transaction.transactionproxyfailed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService self;

    @PostConstruct
    public void init() {
        log.info("this {} self {}", this.getClass().getName(), self.getClass().getName());
    }

    //私有方法
    public int createUserWrong1(String name) {
        try {
            this.createUserPrivate(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }

    //自调用
    public int createUserWrong2(String name) {
        try {
            this.createUserPublic(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }

    /**
     * @Transactional 生效原则 1，除非特殊配置（比如使用 AspectJ 静态织入实现 AOP），否则只有定义在 public 方法上的 @Transactional 才能生效。
     * 原因是，Spring 默认通过动态代理的方式实现 AOP，对目标方法进行增强，private 方法无法代理到，Spring 自然也无法动态增强事务处理逻辑。
     * @param entity
     */
    @Transactional
    private void createUserPrivate(UserEntity entity) {
        userRepository.save(entity);
        if (entity.getName().contains("test"))
            throw new RuntimeException("invalid username!");
    }

    /**
     * 代理依然不生效，必须通过代理过的类从外部调用目标方法才能生效
     * @param entity
     */
    //可以传播出异常
    @Transactional
    public void createUserPublic(UserEntity entity) {
        userRepository.save(entity);
        if (entity.getName().contains("test"))
            throw new RuntimeException("invalid username!");
    }

    //重新注入自己
    public int createUserRight(String name) {
        try {
            self.createUserPublic(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }


    //不出异常
    @Transactional
    public int createUserWrong3(String name) {
        try {
            this.createUserPublic(new UserEntity(name));
        } catch (Exception ex) {
            log.error("create user failed because {}", ex.getMessage());
        }
        return userRepository.findByName(name).size();
    }

    public int getUserCount(String name) {
        return userRepository.findByName(name).size();
    }
}
