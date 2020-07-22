/*
 * Copyright (c) 2020. The Open Data Management Platform contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opendmp.dataflow.config

import org.apache.camel.component.pulsar.utils.AutoConfiguration
import org.apache.camel.component.pulsar.utils.PulsarPath
import org.apache.pulsar.client.admin.PulsarAdmin
import org.apache.pulsar.client.admin.PulsarAdminException
import org.apache.pulsar.common.policies.data.TenantInfo
import org.slf4j.LoggerFactory

class PulsarAutoConfig(
        val admin: PulsarAdmin?,
        val clusters: Set<String>) : AutoConfiguration(admin,clusters) {

    private val LOGGER = LoggerFactory.getLogger(AutoConfiguration::class.java)

    override fun ensureNameSpaceAndTenant(path: String?) {
        if (admin != null) {
            val pulsarPath = PulsarPath(path)
            if (pulsarPath.isAutoConfigurable) {
                val tenant = pulsarPath.tenant
                val namespace = pulsarPath.namespace
                try {
                    ensureTenant(tenant)
                    ensureNameSpace(tenant, namespace)
                } catch (var6: PulsarAdminException) {
                    LOGGER.error(var6.message)
                }
            }
        }
    }

    @Throws(PulsarAdminException::class)
    private fun ensureNameSpace(tenant: String, namespace: String) {
        val namespaces = admin?.namespaces()?.getNamespaces(tenant)
        if (!namespaces!!.contains("$tenant/$namespace")) {
            admin?.namespaces()?.createNamespace(namespace, clusters)
        }
    }

    @Throws(PulsarAdminException::class)
    private fun ensureTenant(tenant: String) {
        val tenants = admin?.tenants()
        val tenantNames = tenants?.tenants
        if (!tenantNames!!.contains(tenant)) {
            val tenantInfo = TenantInfo()
            tenantInfo.allowedClusters = clusters
            admin?.tenants()?.createTenant(tenant, tenantInfo)
        }
    }

}