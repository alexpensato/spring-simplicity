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
package org.pensatocode.simplicity.jdbc.sql

import org.pensatocode.simplicity.jdbc.TableDescription
import org.springframework.data.domain.Pageable

import java.sql.DatabaseMetaData
import java.sql.SQLException

import java.lang.String.format

/**
 * SQL Generator for Oracle up to 11g. If you have 12g or newer, then use
 * [SQL2008SqlGenerator].

 * @see [
 * Oracle: ROW_NUMBER vs ROWNUM](https://explainextended.com/2009/05/06/oracle-row_number-vs-rownum/)
 */
class Oracle9SqlGenerator : DefaultSqlGenerator() {

    @Throws(SQLException::class)
    override fun isCompatible(metadata: DatabaseMetaData): Boolean {
        return "Oracle" == metadata.databaseProductName
    }

    override fun selectAll(table: TableDescription, page: Pageable): String {
        val sort = if (page.sort.isSorted) page.sort else sortByPKs(table.pkColumns)

        return format("SELECT t2__.* FROM ( "
                + "SELECT t1__.*, ROWNUM as rn__ FROM ( %s ) t1__ "
                + ") t2__ WHERE t2__.rn__ > %d AND ROWNUM <= %d",
                selectAll(table, sort), page.offset, page.pageSize)
    }

    override fun selectAll(table: TableDescription, whereClause: String, page: Pageable): String {
        val sort = if (page.sort.isSorted) page.sort else sortByPKs(table.pkColumns)

        return format("SELECT t2__.* FROM ( "
                + "SELECT t1__.*, ROWNUM as rn__ FROM ( %s ) t1__ "
                + ") t2__ WHERE t2__.rn__ > %d AND ROWNUM <= %d",
                selectAll(table, whereClause, sort), page.offset, page.pageSize)
    }
}
