import applications from "./applications-science-3.png"
import system from "./system-run-5.png"
import view from "./view-list-tree-4.png"
import picker from "./color-picker-grey.png"
import preferences from "./preferences-system-4.png"
import pim from "./view-pim-tasks.png"
import chart from "./office-chart-area.png"
import appointment from "./appointment-new-3.png"
import input from "./document-import-2_custom.png"
import output from "./document-export-4_custom.png"
import preview from "./document-preview.png"

export default new Map([
    ["Root", system],
    ["TestFunc", system],
    ["BeforeProcess", applications],
    ["BeforeThread", applications],
    ["AfterThread", applications],
    ["AfterProcess", applications],
    ["Before", applications],
    ["After", applications],
    ["HTTPRequest", picker],
    ["JARImport", picker],
    ["TransactionController", view],
    ["OnceOnlyController", view],
    ["IfController", view],
    ["LoopController", view],
    ["WhileController", view],
    ["CSVDataSetConfig", preferences],
    ["HTTPHeaderManager", preferences],
    ["HTTPRequestDefaults", preferences],
    ["Counter", preferences],
    ["DNSCacheManager", preferences],
    ["KeystoreConfiguration", pim],
    ["SummaryReport", chart],
    ["ConstantTimer", appointment],
    ["HTTPURLRewritingModifier", input],
    ["JSR223PreProcessor", pim],
    ["JSR223PostProcessor", pim],
    ["BeanShellPostProcessor", pim],
    ["RegularExpressionExtractor", output],
    ["ResponseAssertion", preview],
])