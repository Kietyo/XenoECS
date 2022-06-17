package com.xenotactic.ecs

abstract class ECSException(
        messageFn: () -> String
): RuntimeException(messageFn())

class ECSComponentNotFoundException(messageFn: () -> String) : ECSException(messageFn)

class SingletonInjectionAlreadyExistsException(messageFn: () -> String) : ECSException(messageFn)