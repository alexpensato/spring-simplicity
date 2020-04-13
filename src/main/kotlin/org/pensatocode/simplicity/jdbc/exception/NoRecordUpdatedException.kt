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
package org.pensatocode.simplicity.jdbc.exception

import org.springframework.dao.IncorrectUpdateSemanticsDataAccessException

import java.lang.String.format
import org.springframework.util.StringUtils.arrayToCommaDelimitedString

/**
 * Exception thrown when trying to update a record that doesn't exist.
 */
class NoRecordUpdatedException : IncorrectUpdateSemanticsDataAccessException {

    val tableName: String
    private val id: Array<Any>


    constructor(tableName: String, id: Array<Any>) : super(format("No record with id = {%s} exists in table %s",
            arrayToCommaDelimitedString(id), tableName)) {
        this.tableName = tableName
        this.id = id.clone()
    }

    constructor(tableName: String, msg: String) : super(msg) {
        this.tableName = tableName
        this.id = arrayOf<Any>(0)
    }

    constructor(tableName: String, msg: String, cause: Throwable) : super(msg, cause) {
        this.tableName = tableName
        this.id = arrayOf<Any>(0)
    }


    override fun wasDataUpdated(): Boolean {
        return false
    }

    fun getId(): Array<Any> {
        return id.clone()
    }
}
