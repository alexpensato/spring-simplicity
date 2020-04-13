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
package org.pensatocode.simplicity.jdbc.mapper

interface ReadOnlyRowMapper<T> : TransactionalRowMapper<T> {

    override fun mapColumns(entity: T): Map<String, Any> {
        throw UnsupportedOperationException(
                "This repository is read-only, it can't create or update entities.")
    }

    override fun columnsValues(entity: T, columns: Array<String>): Array<Any> {
        throw UnsupportedOperationException(
                "This repository is read-only, it can't create or update entities.")
    }
}
