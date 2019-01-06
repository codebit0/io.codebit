/**
 * Generated file date : {{now}}
 */
package {{package.getQualifiedName}};

import java.lang.Override;
import javax.annotation.Generated;

import LambdaLogger;
import LambdaLoggerFactory;

@Generated(value="{{generatedProcessor}}", date="{{now}}", comments = "{{package.getQualifiedName}}.{{& className}}")
privileged aspect {{& aspectClassName}} {

    private final static LambdaLogger {{& className}}.log = LambdaLoggerFactory.getLogger("ROOT");

}
