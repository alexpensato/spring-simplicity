package net.pensato.simplicity

import org.mockito.Mockito

/**
 * Created by Alex on 09/12/2016.
 */

inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java)
