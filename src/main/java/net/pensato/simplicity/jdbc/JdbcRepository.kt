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

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.NoRepositoryBean
import java.io.Serializable

/**
 * JDBC specific extension of [org.springframework.data.repository.Repository].
 * @param <T> the domain type the repository manages.
 * @param <ID> the type of the id of the entity the repository manages.
 */
@NoRepositoryBean
interface JdbcRepository<T: Any, ID : Serializable> {

    fun count(): Long

    fun exists(id: ID): Boolean

    fun findAll(): List<T>

    fun findAll(sort: Sort): List<T>

    fun findAll(pageable: Pageable): Page<T>

    fun findAll(whereClause: String): List<T>

    fun findAll(whereClause: String, sort: Sort): List<T>

    fun findAll(whereClause: String, pageable: Pageable): Page<T>

    fun findAll(ids: Iterable<ID>): List<T>

    fun findOne(id: Any): T?

    fun delete(id: Any): Int

    fun <S : T> save(entity: S): S

    fun <S : T> save(entities: Iterable<S>): List<S>

    fun <S : T> create(entity: S): ID?

    fun <S : T> update(entity: S): Int
}