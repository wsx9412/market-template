package com.template.market.domain.usecase

import com.template.market.domain.model.Post
import com.template.market.domain.repository.PostRepository
import javax.inject.Inject

class GetPostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(id: String): Post? = repository.getById(id)
}
