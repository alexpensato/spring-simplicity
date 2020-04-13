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

import org.springframework.data.mapping.PersistentEntity
import org.springframework.data.repository.core.support.PersistentEntityInformation
import org.springframework.util.ClassUtils
import java.io.Serializable
import java.util.regex.Pattern


fun repeat(text: String, delimiter: String, count: Int): String {
    val sb = StringBuilder()
    for(i in 2..count) {
        sb.append("$text$delimiter")
    }
    sb.append(text)
    return sb.toString()
}

fun convertToCamelCase(input: String): String {
    val pattern = Pattern.compile("(_)([a-z])")

        val matcher = pattern.matcher(input)
        val sb = StringBuilder()
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(2).toUpperCase())
        }
        matcher.appendTail(sb)
        return sb.toString()
}

fun convertToSnakeCase(input: String): String {
    val pattern = Pattern.compile("(.)(\\p{Upper})")

        val matcher = pattern.matcher(input)
        val replacementPattern = "$1_$2"
        return matcher.replaceAll(replacementPattern).toLowerCase()
}
