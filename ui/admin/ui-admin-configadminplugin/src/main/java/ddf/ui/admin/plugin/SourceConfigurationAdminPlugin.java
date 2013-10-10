/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
package ddf.ui.admin.plugin;

import ddf.catalog.CatalogFramework;
import ddf.catalog.operation.SourceInfoRequestEnterprise;
import ddf.catalog.operation.SourceInfoResponse;
import ddf.catalog.service.ConfiguredService;
import ddf.catalog.source.FederatedSource;
import ddf.catalog.source.SourceDescriptor;
import ddf.catalog.source.SourceUnavailableException;
import ddf.ui.admin.api.plugin.ConfigurationAdminPlugin;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Scott Tustison
 */
public class SourceConfigurationAdminPlugin implements ConfigurationAdminPlugin
{
    private final XLogger logger = new XLogger(LoggerFactory.getLogger(SourceConfigurationAdminPlugin.class));
    private CatalogFramework catalogFramework;

    public SourceConfigurationAdminPlugin()
    {

    }

    public void init()
    {
    }

    public void destroy()
    {
    }

    public CatalogFramework getCatalogFramework()
    {
        return catalogFramework;
    }

    public void setCatalogFramework(CatalogFramework catalogFramework)
    {
        this.catalogFramework = catalogFramework;
    }

    /**
     * Returns a map of configuration data that should be appended to the configurationDataMap parameter. The configurationDataMap
     * that is passed into this function is unmodifiable and is passed in to simply expose what information already exists.
     * @param configurationPid service.pid for the ConfigurationAdmin configuration
     * @param configurationDataMap map of what properties have already been added to the configuration in question
     * @param bundleContext used to retrieve list of services
     * @return Map defining additional properties to add to the configuration
     */
    @Override
    public Map<String, Object> getConfigurationData(String configurationPid, Map<String, Object> configurationDataMap, BundleContext bundleContext)
    {
        Map<String, Object> statusMap = new HashMap<String, Object>();
        try
        {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(FederatedSource.class.getCanonicalName(), null);
            if (refs != null)
            {
                for (ServiceReference ref : refs)
                {
                    Object superService = bundleContext.getService(ref);
                    //it should already be an instance of FederatedSource since that's what we asked for, but doesn't
                    //hurt to check it again
                    if (superService instanceof FederatedSource && superService instanceof ConfiguredService)
                    {
                        ConfiguredService cs = (ConfiguredService) superService;

                        if (StringUtils.isNotEmpty(cs.getConfigurationPid()) && cs.getConfigurationPid().equals(configurationPid))
                        {
                            if(catalogFramework != null)
                            {
                                SourceInfoResponse response = catalogFramework.getSourceInfo(new SourceInfoRequestEnterprise(true));
                                Set<SourceDescriptor> sources = response.getSourceInfo();
                                for(SourceDescriptor descriptor : sources)
                                {
                                    if(descriptor.getSourceId().equals(((FederatedSource) superService).getId()))
                                    {
                                        statusMap.put("available", descriptor.isAvailable());
                                    }
                                }
                            }
                            else
                            {
                                //we don't want to call isAvailable because that can potentially block execution
                                //but if for some reason we have no catalog framework, just hit the source directly
                                statusMap.put("available", ((FederatedSource) superService).isAvailable());
                            }
                        }
                    }
                }
            }
        }
        catch (org.osgi.framework.InvalidSyntaxException e)
        {
            //this should never happen because the filter is always null
            logger.error("Error reading LDAP service filter", e);
        }
        catch (SourceUnavailableException e)
        {
            logger.error("Unable to retrieve sources from Catalog Framework", e);
        }
        return statusMap;
    }
}