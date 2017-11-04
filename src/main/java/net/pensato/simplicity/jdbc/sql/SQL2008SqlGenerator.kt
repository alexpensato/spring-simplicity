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
package net.pensato.simplicity.jdbc.sql

import net.pensato.simplicity.jdbc.TableDescription
import org.springframework.data.domain.Pageable

import java.sql.DatabaseMetaData
import java.sql.SQLException

import java.lang.String.format

/**
 * SQL Generator for DB servers that support the SQL:2008 standard OFFSET
 * feature: Apache Derby, Microsoft SQL Server 2012, and Oracle 12c.
 */
class SQL2008SqlGenerator : DefaultSqlGenerator() {

    @Throws(SQLException::class)
    override fun isCompatible(metadata: DatabaseMetaData): Boolean {
        val productName = metadata.databaseProductName
        val majorVersion = metadata.databaseMajorVersion

        return "Apache Derby" == productName
                || "Oracle" == productName && majorVersion >= 12
                || "Microsoft SQL Server" == productName && majorVersion >= 11  // >= 2012
    }

    override fun selectAll(table: TableDescription, page: Pageable): String {
        val sort = if (page.sort != null) page.sort else sortByPKs(table.pkColumns)

        return format("%s OFFSET %d ROWS FETCH NEXT %d ROW ONLY",
                selectAll(table, sort), page.offset, page.pageSize)
    }

    override fun selectAll(table: TableDescription, whereClause: String, page: Pageable): String {
        val sort = if (page.sort != null) page.sort else sortByPKs(table.pkColumns)

        return format("%s OFFSET %d ROWS FETCH NEXT %d ROW ONLY",
                selectAll(table, whereClause, sort), page.offset, page.pageSize)
    }
}
