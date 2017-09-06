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
import java.util.Arrays.asList

/**
 * SQL Generator for DB servers that support LIMIT ... OFFSET clause:
 * PostgreSQL, H2, HSQLDB, SQLite, MariaDB, and MySQL.
 */
class LimitOffsetSqlGenerator : DefaultSqlGenerator() {


    @Throws(SQLException::class)
    override fun isCompatible(metadata: DatabaseMetaData): Boolean {
        return SUPPORTED_PRODUCTS.contains(metadata.databaseProductName)
    }

    override fun selectAll(table: TableDescription, page: Pageable): String {
        return format("%s LIMIT %d OFFSET %d",
                selectAll(table, page.sort), page.pageSize, page.offset)
    }

    companion object {

        private val SUPPORTED_PRODUCTS = asList("PostgreSQL", "H2", "HSQL Database Engine", "MySQL")
    }
}
