/**
 * Generated file date : {{now}}
 */
package {{package.getQualifiedName}};

import java.lang.Override;
import javax.annotation.Generated;

@Generated(value="{{generatedProcessor}}", date="{{now}}", comments = "{{package.getQualifiedName}}.{{& className}}")
privileged aspect {{& aspectClassName}} {
    @Override
    public String {{& element.asType}} {{& className}}.toString() {
        StringBuilder $sb = new StringBuilder(128 * 3);
        $sb.append('{');
        {{#fields}}

        {{/fields}}
        if($sb.length() > 1) {
            //,\n제거
            $sb.delete($sb.length()-2, $sb.length());
        }
        $sb.append('}');
        return {{{toString}}};
    }
}
