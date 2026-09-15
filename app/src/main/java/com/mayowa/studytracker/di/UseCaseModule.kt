package com.mayowa.studytracker.di

import com.mayowa.studytracker.domain.usecase.BuildStudyPlanUseCase
import com.mayowa.studytracker.domain.usecase.CalculateAdaptiveGoalUseCase
import com.mayowa.studytracker.domain.usecase.CalculateStreakUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides fun provideCalculateStreakUseCase(): CalculateStreakUseCase = CalculateStreakUseCase()
    @Provides fun provideCalculateAdaptiveGoalUseCase(): CalculateAdaptiveGoalUseCase = CalculateAdaptiveGoalUseCase()
    @Provides fun provideBuildStudyPlanUseCase(): BuildStudyPlanUseCase = BuildStudyPlanUseCase()
}
