package com.jininsadaecheonmyeong.starthubserver.event.bmc

import com.jininsadaecheonmyeong.starthubserver.entity.bmc.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.entity.user.User
import org.springframework.context.ApplicationEvent

class BmcCreatedEvent(
    source: Any,
    val businessModelCanvas: BusinessModelCanvas,
    val user: User,
) : ApplicationEvent(source)
