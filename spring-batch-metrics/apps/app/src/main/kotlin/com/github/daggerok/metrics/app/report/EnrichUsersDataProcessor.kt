package com.github.daggerok.metrics.app.report

import com.github.daggerok.metrics.app.PaymentsReport
import com.github.daggerok.user.client.UserClient
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@StepScope
@Component
class EnrichUsersDataProcessor(
    @Value("#{jobParameters['dateTime']}") val dateTime: String,
    private val userClient: UserClient,
) : ItemProcessor<PaymentsReport, PaymentsReport> {

    override fun process(item: PaymentsReport): PaymentsReport? =
        userClient.getUser(item.userId).run {
            // item.apply {
            //     userFullName = "$firstName $lastName"
            //     userRegistrationDate = createdAt?.toLocalDate()
            // }
            item.copy(
                userFullName = "$firstName $lastName",
                userRegistrationDate = createdAt?.toLocalDate(),
            )
        }
}
