import {FieldType} from "./field-type";

export interface Field {

    name: string;
    defaultMap: string;
    label: string;
    required: boolean;
    requiredText: string | null;
    type: FieldType;
    defaultValue: string | null;
    options: string[] | null;
    allowStaticValue: boolean;

}
