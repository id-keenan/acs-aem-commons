/*
 * #%L
 * ACS AEM Commons Bundle
 * %%
 * Copyright (C) 2019 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.commons.properties.impl;

import com.adobe.acs.commons.properties.PropertyAggregatorService;
import io.wcm.testing.mock.aem.junit.AemContext;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.adobe.acs.commons.properties.TemplatedDialogTestUtil.defaultConfigMap;
import static com.adobe.acs.commons.properties.TemplatedDialogTestUtil.defaultService;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PropertyAggregatorServiceImplTest {
    @Rule
    public final AemContext context = new AemContext(ResourceResolverType.JCR_OAK);

    private PropertyAggregatorService service;

    @Before
    public void setup() {
        context.load().json(getClass().getResourceAsStream("PropertyAggregatorServiceContent.json"), "/content/we-retail/language-masters/en/experience");
    }

    @Test
    public void testAggregationOfPageResource() {
        service = defaultService(context);

        Resource lofoten = context.resourceResolver().getResource("/content/we-retail/language-masters/en/experience/arctic-surfing-in-lofoten");
        context.request().setResource(lofoten);
        Map<String, Object> properties = service.getProperties(context.request());
        Map<String, Object> expected = defaultPropertyMap();
        assertEquals(expected, properties);
    }

    @Test
    public void testAggregationOfComponentOnPage() {
        service = defaultService(context);

        Resource lofoten = context.resourceResolver().getResource("/content/we-retail/language-masters/en/experience/arctic-surfing-in-lofoten/jcr:content/root/hero_image");
        context.request().setResource(lofoten);
        Map<String, Object> properties = service.getProperties(context.request());
        Map<String, Object> expected = defaultPropertyMap();
        assertEquals(expected, properties);
    }


    @Test
    public void testAggregationOfNonPageResource() {
        service = defaultService(context);
        context.load().json(getClass().getResourceAsStream("PropertyAggregatorServiceAppsContent.json"), "/apps/templated/components/accordion");

        Resource componentResource = context.resourceResolver().getResource("/apps/templated/components/accordion");
        context.request().setResource(componentResource);
        Map<String, Object> properties = service.getProperties(context.request());
        Map<String, Object> expected = new HashMap<>();
        assertEquals(expected, properties);
    }

    @Test
    public void testContentInheritanceOverride() {
        service = defaultService(context);

        Resource lofoten = context.resourceResolver().getResource("/content/we-retail/language-masters/en/experience/arctic-surfing-in-lofoten/jcr:content");
        ModifiableValueMap modifiableValueMap = lofoten.adaptTo(ModifiableValueMap.class);
        // Put in the 'inheritedProperty' value onto the lofoten node itself to be pulled instead of inheriting
        modifiableValueMap.put("inheritedProperty", "newValue");
        context.request().setResource(lofoten);
        Map<String, Object> properties = service.getProperties(context.request());
        Map<String, Object> expected = defaultPropertyMap();
        // Remove old inherited value from default expected
        expected.remove("inherited_page_properties.inheritedProperty");
        // Add overridden value to expected
        expected.put("page_properties.inheritedProperty", "newValue");
        assertEquals(expected, properties);
    }

    @Test
    public void testPropertyExclusion() {
        Map<String, Object> config = defaultConfigMap();
        config.put("exclude.list", new String[]{"cq:(.*)", "jcr:(.*)"});
        context.registerInjectActivateService(new PropertyConfigServiceImpl(), config);
        context.registerInjectActivateService(new AllPagePropertiesContentVariableProvider());
        service = context.registerInjectActivateService(new PropertyAggregatorServiceImpl());

        Resource lofoten = context.resourceResolver().getResource("/content/we-retail/language-masters/en/experience/arctic-surfing-in-lofoten");
        context.request().setResource(lofoten);
        Map<String, Object> properties = service.getProperties(context.request());
        Map<String, Object> expected = new HashMap<>();
        expected.put("inherited_page_properties.inheritedProperty", "inheritedValue");
        expected.put("page_properties.sling:resourceType", "weretail/components/structure/page");
        assertEquals(expected, properties);
    }

    private Map<String, Object> defaultPropertyMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("inherited_page_properties.inheritedProperty", "inheritedValue");
        map.put("page_properties.jcr:primaryType", "cq:PageContent");
        map.put("page_properties.jcr:title", "Arctic Surfing In Lofoten");
        map.put("page_properties.sling:resourceType", "weretail/components/structure/page");
        map.put("page_properties.jcr:createdBy", "admin");
        return map;
    }
}
