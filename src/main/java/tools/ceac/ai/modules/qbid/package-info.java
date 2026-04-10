/**
 * qBid runtime module.
 *
 * <p>This package contains the local qBid scraping runtime embedded by the desktop launcher.
 * qBid credentials remain local to this runtime and never travel to the central control plane.
 *
 * <p>The module now follows the same broad shape as the other CEAC AI Tools modules:
 *
 * <ul>
 *   <li>{@code application}: qBid-specific use cases, session orchestration and outbound ports</li>
 *   <li>{@code domain}: domain models and exceptions exposed by the runtime</li>
 *   <li>{@code infrastructure}: HTTP scraping client and HTML parsers</li>
 *   <li>{@code interfaces}: REST, MCP and OAuth entrypoints</li>
 * </ul>
 */
package tools.ceac.ai.modules.qbid;


