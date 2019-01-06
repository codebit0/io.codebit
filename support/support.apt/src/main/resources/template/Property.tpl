/**
 * Generated file date : {{now}}
 */
package {{package.getQualifiedName}};

import javax.annotation.Generated;

@Generated(value="{{generatedProcessor}}", date="{{now}}", comments = "{{& classNameQualified}}")
privileged aspect {{& aspectClassName}} {
    {{#value}}
    //Serializable 선언
    declare parents : {{& classNameQualified}} implements java.io.Serializable, Cloneable;

    @java.lang.Override
    public boolean {{& classNameQualified}}.equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof {{& classNameQualified}})) return false;
        final {{& classNameQualified}} other = ({{& classNameQualified}})o;
        {{#getters}}
        if(!java.util.Objects.deepEquals(this.{{& element.getSimpleName}}, other.{{& element.getSimpleName}})) {
            return false;
        }
        {{/getters}}
        return true;
    }

    @java.lang.Override
    public int {{& classNameQualified}}.hashCode() {
        return java.util.Objects.hash({{#commaTrim}}{{#getters}}this.{{& element.getSimpleName}}, {{/getters}}{{/commaTrim}});
    }
    {{/value}}

    {{#getters}}
    {{{comment}}}
    {{modifier}} {{& element.asType}} {{& classNameQualified}}.{{& methodName}}() {
        return this.{{& element.getSimpleName}};
    }
    {{/getters}}
    {{#setters}}
    {{{comment}}}
    {{modifier}} {{#config.chain}}{{& classNameQualified}}{{/config.chain}}{{^config.chain}}void{{/config.chain}} {{& classNameQualified}}.{{& methodName}}({{& element.asType}} {{& element.getSimpleName}}) {
        this.{{& element.getSimpleName}} = {{& element.getSimpleName}};
        {{#config.chain}}return this;{{/config.chain}}
    }
    {{/setters}}
}
