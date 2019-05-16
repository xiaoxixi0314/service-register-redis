# use redis register service

##1. Need follow config in property file
```properties
# enable register service
# if it's false, will skip to register service to redis
register.service.enabled=true
# redis host.
register.service.redis.host=127.0.0.1
# redis port, default is 6379.
# if your redis server port is 6379,
# need not set this property.
register.service.redis.port=6379
# redis access password.
# if your redis server not set password,
# need not set this property
register.service.redis.pwd=111111
# service prifix, like maven dependency group id
register.service.prefix=com.xiaoxixi.service
# your service name
register.service.name=user
# your service version,default is v1
# if your service version is v1,
# need not set this property
register.service.version=v1

```
    

