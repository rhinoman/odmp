package io.opendmp.processor.config

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