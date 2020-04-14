/*
 * Copyright 2017-2020 Alex Magalhaes <alex@pensatocode.org>
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
package org.pensatocode.simplicity.web

import org.pensatocode.simplicity.jdbc.JdbcRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.*
import java.io.Serializable

@RestController
abstract class AbstractController<T: Any, ID : Serializable>
@Autowired constructor(var repository: JdbcRepository<T, ID>)
{
    @GetMapping
    open fun findAll(pageable: Pageable): Page<T> {
        return repository.findAll(pageable)
    }

    @GetMapping("/count")
    @ResponseBody
    open fun count(): Long {
        return repository.count()
    }

    @GetMapping("/{id}")
    @ResponseBody
    open fun findById(@PathVariable id: ID?): T? {
        Assert.notNull(id, "You must provide an ID to locate an item in the repository.")
        return repository.findOne(id!!)
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    open fun delete(@PathVariable id: ID?): String {
        Assert.notNull(id, "You must provide an ID to delete an item from the repository.")
        return "Total itens deleted: ${repository.delete(id!!)}."
    }

    @PutMapping("/{id}")
    @ResponseBody
    open fun update(@PathVariable id: ID?, @RequestBody t: T): String {
        Assert.notNull(id, "You must provide an ID to update an item in the repository.")
        return "Total itens updated: ${repository.update(t, id)}."
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    open fun insert(@RequestBody t: T): T {
        return repository.save(t)
    }

}
