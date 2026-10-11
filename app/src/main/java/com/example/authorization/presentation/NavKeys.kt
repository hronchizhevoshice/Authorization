package com.example.authorization.presentation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object ChoiceKey : NavKey
@Serializable data object LoginKey : NavKey
@Serializable data object RegisterKey : NavKey
@Serializable data object HomeKey : NavKey
@Serializable data object ProfileKey : NavKey
@Serializable data object VehiclesKey : NavKey
@Serializable data object OrdersKey : NavKey