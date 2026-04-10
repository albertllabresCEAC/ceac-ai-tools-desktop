/**
 * Launcher orchestration layer.
 *
 * <p>This package is responsible for:
 *
 * <ul>
 *   <li>desktop login against the control plane</li>
 *   <li>bootstrap and session handling</li>
 *   <li>local tunnel lifecycle</li>
 *   <li>startup and shutdown of embedded resource modules</li>
 *   <li>shared runtime helpers that keep Outlook, Campus and qBid operationally symmetric</li>
 * </ul>
 *
 * <p>The launcher package belongs to the desktop shell. It is intentionally separate from
 * {@code tools.ceac.ai.modules.*}, which contains the resource-specific runtime code.
 */
package tools.ceac.ai.desktop.launcher;

