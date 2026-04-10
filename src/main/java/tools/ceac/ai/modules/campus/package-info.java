/**
 * Campus runtime module.
 *
 * <p>This package contains the Campus runtime, including the embedded-browser login flow and the
 * HTTP session reuse needed to expose Campus data through REST and MCP.
 *
 * <p>The Campus module follows the same broad vocabulary as the other modules:
 *
 * <ul>
 *   <li>{@code application}: use cases and outbound ports</li>
 *   <li>{@code domain}: Campus-facing models and exceptions</li>
 *   <li>{@code infrastructure}: browser bridge, HTTP client and Moodle parsers</li>
 *   <li>{@code interfaces}: REST, MCP, OAuth and embedded desktop UI entrypoints</li>
 * </ul>
 */
package tools.ceac.ai.modules.campus;


