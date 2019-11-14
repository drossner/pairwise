package de.iisys.va.pairwise.javalinvueextensions

import io.javalin.plugin.rendering.vue.VueComponent

fun componentwithProps(componentName: String, propsMap: Map<String, String> = mapOf()): VueComponent{
    val sb = StringBuilder()
    propsMap.entries.forEach {
        //sb.append(' ').append(it.key).append('=').append('\'').append(it.value).append('\'')
        sb.append(' ').append(it.key).append('=').append(it.value)
    }
    return VueComponent("<$componentName${sb.toString()}></$componentName>")
}