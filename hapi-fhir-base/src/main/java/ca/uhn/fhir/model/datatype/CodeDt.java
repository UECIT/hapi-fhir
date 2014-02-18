package ca.uhn.fhir.model.datatype;

import ca.uhn.fhir.model.api.BasePrimitiveDatatype;
import ca.uhn.fhir.model.api.ICodeEnum;
import ca.uhn.fhir.model.api.annotation.DatatypeDef;

@DatatypeDef(name="code")
public class CodeDt<T extends ICodeEnum> extends BasePrimitiveDatatype implements ICodedDatatype {

}
