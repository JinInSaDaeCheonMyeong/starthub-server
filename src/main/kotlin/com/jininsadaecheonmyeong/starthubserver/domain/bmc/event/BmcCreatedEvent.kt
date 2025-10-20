package com.jininsadaecheonmyeong.starthubserver.domain.bmc.event

import com.jininsadaecheonmyeong.starthubserver.domain.bmc.entity.BusinessModelCanvas
import com.jininsadaecheonmyeong.starthubserver.domain.user.entity.User
import org.springframework.context.ApplicationEvent

class BmcCreatedEvent(
    source: Any,
    val businessModelCanvas: BusinessModelCanvas,
    val user: User,
) : ApplicationEvent(source)
