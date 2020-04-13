/*
 * Copyright 2017-2020 twitter.com/PensatoAlex
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pensatocode.simplicity.extra

import org.springframework.util.ObjectUtils
import java.util.*

@Suppress("UNCHECKED_CAST")
fun <T> Any.toArray(): Array<T>  = if (this is Array<*>) {
        if (this[0] is Any)
            this as Array<T>
        else
            ObjectUtils.toObjectArray(this) as Array<T>
    } else {
        arrayOf(this) as Array<T>
    }

fun <T> Iterable<T>.toList(): List<T> = if (this is List<*>) {
        this as List<T>
    } else {
        val result = ArrayList<T>()
        for (item in this) {
            result.add(item)
        }
        result
    }

fun <T> Array<T>.printAsString(): String = if (this.isNotEmpty()) {
    val sj = StringJoiner(",")
    for(a in this) {
        sj.add(a.toString())
    }
    sj.toString()
} else {
    ""
}

