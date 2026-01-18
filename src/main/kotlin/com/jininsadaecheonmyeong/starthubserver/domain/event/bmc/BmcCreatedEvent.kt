package com.jininsadaecheonmyeong.starthubserver.domain.event.bmc

import com.jininsadaecheonmyeong.starthubserver.domain.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.entity.user.User
import org.springframework.context.ApplicationEvent

class BmcCreatedEvent(
    source: Any,
    val businessModelCanvas: BusinessModelCanvas,
    val user: User,
) : ApplicationEvent(source)
