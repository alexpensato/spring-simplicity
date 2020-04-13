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

import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessResourceFailureException

import javax.sql.DataSource
import java.sql.DatabaseMetaData
import java.sql.SQLException
import java.util.ArrayDeque
import java.util.WeakHashMap

object SqlGeneratorFactory {

    private val LOG = LoggerFactory.getLogger(SqlGeneratorFactory::class.java)

    private val generators = ArrayDeque<SqlGenerator>()

    private val cache = WeakHashMap<DataSource, SqlGenerator>(2, 1.0f)


    init {
        registerGenerator(DefaultSqlGenerator())
        registerGenerator(LimitOffsetSqlGenerator())
        registerGenerator(SQL2008SqlGenerator())
        registerGenerator(Oracle9SqlGenerator())
    }

    /**
     * @param dataSource The DataSource for which to find compatible SQL Generator.
     * @return An SQL Generator compatible with the given `dataSource`.
     * @throws DataAccessResourceFailureException if exception is thrown when
     *         trying to obtain Connection or MetaData from the `dataSource`.
     * @throws IllegalStateException if no compatible SQL Generator is found.
     */
    fun getGenerator(dataSource: DataSource): SqlGenerator {
        if (cache.containsKey(dataSource)) {
            return cache[dataSource] as SqlGenerator
        }

        val metaData: DatabaseMetaData
        try {
            metaData = dataSource.connection.metaData
        } catch (ex: SQLException) {
            throw DataAccessResourceFailureException(
                    "Failed to retrieve database metadata", ex)
        }

        for (generator in generators) {
            try {
                if (generator.isCompatible(metaData)) {
                    LOG.info("Using SQL Generator {} for dataSource {}",
                            generator.javaClass.name, dataSource.javaClass)

                    cache.put(dataSource, generator)
                    return generator
                }
            } catch (ex: SQLException) {
                LOG.warn("Exception occurred when invoking isCompatible() on {}",
                        generator.javaClass.simpleName, ex)
            }

        }

        // This should not happen, because registry should always contain one
        // "default" generator that returns true for every DatabaseMetaData.
        throw IllegalStateException("No compatible SQL Generator found.")
    }

    /**
     * Adds the `sqlGenerator` to the top of the generators registry.

     * @param sqlGenerator The SQL Generator instance to register.
     */
    fun registerGenerator(sqlGenerator: SqlGenerator) {
        generators.push(sqlGenerator)
    }

    /**
     * Removes all generators from the factory's registry.
     */
    fun clear() {
        generators.clear()
        cache.clear()
    }
}
