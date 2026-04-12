/**
 * Trello runtime module.
 *
 * <p>This package contains the local Trello runtime embedded by the desktop launcher. The launcher
 * captures the operator's Trello token through a browser callback on localhost and injects it into
 * this runtime without sending it to the control plane.
 *
 * <p>The module follows the same broad package shape as the other CEAC AI Tools runtimes:
 *
 * <ul>
 *   <li>{@code application}: Trello-specific use cases and runtime services</li>
 *   <li>{@code domain}: Trello-facing models and exceptions</li>
 *   <li>{@code infrastructure}: HTTP client and persistence adapters local to the runtime</li>
 *   <li>{@code interfaces}: REST, MCP and OAuth entrypoints</li>
 * </ul>
 */
package tools.ceac.ai.modules.trello;
