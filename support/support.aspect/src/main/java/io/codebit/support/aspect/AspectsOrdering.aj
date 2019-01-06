package io.codebit.support.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;

@Aspect
@DeclarePrecedence("AdviceEvent, InjectProcessor, ValidProcessor")
public class AspectsOrdering {
}
