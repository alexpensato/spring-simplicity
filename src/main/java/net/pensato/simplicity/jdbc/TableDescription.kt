/*
 * Copyright 2017 twitter.com/PensatoAlex
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
package net.pensato.simplicity.jdbc

class TableDescription {

    val tableName: String
    val selectClause: String
    val fromClause: String
    val pkColumns: Array<String>
    var columns: Array<String>

    constructor(tableName: String, columns: Array<String>, selectClause: String?, fromClause: String?, vararg pkColumns: String) {
        this.tableName = tableName
        this.selectClause = selectClause ?: "*"
        this.fromClause = fromClause ?: tableName
        this.pkColumns = if (pkColumns.isEmpty()) arrayOf(*pkColumns) else arrayOf("id")
        this.columns = columns
    }

    constructor(tableName: String, columns: Array<String>, fromClause: String?, vararg pkColumns: String) :
            this(tableName, columns, null, fromClause, *pkColumns) {}

    constructor(tableName: String, columns: Array<String>, vararg pkColumns: String) :
            this(tableName, columns, null, *pkColumns) {

    }

}
