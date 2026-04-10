/**
 * Outlook runtime module.
 *
 * <p>This package contains the Outlook-specific Spring runtime and supporting code. It does not
 * own the desktop shell or product-wide orchestration.
 *
 * <p>The Outlook module now follows the same broad package vocabulary as the other CEAC AI Tools
 * modules:
 *
 * <ul>
 *   <li>{@code application}: use-case services and outbound ports</li>
 *   <li>{@code domain}: Outlook-facing models and exceptions</li>
 *   <li>{@code infrastructure}: JACOB/COM integration</li>
 *   <li>{@code interfaces}: REST, MCP and OAuth entrypoints</li>
 * </ul>
 */
package tools.ceac.ai.modules.outlook;


