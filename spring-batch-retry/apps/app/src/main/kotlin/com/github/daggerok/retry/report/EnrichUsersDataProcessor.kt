package com.github.daggerok.retry.report

import com.github.daggerok.retry.PaymentsReport
import com.github.daggerok.user.client.UserClient
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@StepScope
@Component
class EnrichUsersDataProcessor(
    private val userClient: UserClient,
    @Value("#{jobParameters['dateTime']}") val dateTime: String,
) : ItemProcessor<PaymentsReport, PaymentsReport> {

    @Retryable
    override fun process(item: PaymentsReport): PaymentsReport? =
        userClient.getUser(item.userId).run {
            item.copy(
                userFullName = "$firstName $lastName",
                userRegistrationDate = createdAt?.toLocalDate(),
            )
        }
}
