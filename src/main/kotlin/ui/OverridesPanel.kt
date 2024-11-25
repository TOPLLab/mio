package ui

import com.formdev.flatlaf.extras.FlatSVGIcon
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import javax.swing.Box.Filler
import javax.swing.border.EmptyBorder

class OverridesPanel : JScrollPane() {
    private val box = Box.createVerticalBox()
    private val glue: Filler = (Box.createVerticalGlue() as Filler).apply {
        changeShape(
            minimumSize,
            Dimension(0, Short.MAX_VALUE.toInt()),  // make glue greedy
            maximumSize
        )
    }
    init {
        //val box = JPanel()
        //box.setLayout(BoxLayout(box, BoxLayout.Y_AXIS));

        setViewportView(box)
        /*box.addMouseMotionListener(object : MouseMotionListener {
            override fun mouseDragged(e: MouseEvent) {}

            override fun mouseMoved(e: MouseEvent) {
                if (box.contains(e.point)) {
                    println("true")
                    scrolledPanel.verticalScrollBar.isVisible = true
                } else {
                    println("false")
                    scrolledPanel.verticalScrollBar.isVisible = false
                }
            }
        })*/
        val activeButtons = mutableSetOf<JButton>()
        repeat (50) {
            /*box.add(createItem("chip_digital_read", listOf(1, 2, 3), 5) {

            })*/

            /*panel.addMouseListener(object : MouseListener {
                override fun mouseClicked(e: MouseEvent?) {
                }

                override fun mousePressed(e: MouseEvent?) {
                }

                override fun mouseReleased(e: MouseEvent?) {
                }

                override fun mouseEntered(e: MouseEvent?) {
                    println("Enter")
                    removeButton.isVisible = true
                    activeButtons.forEach { it.isVisible = false }
                    activeButtons.clear()
                    activeButtons.add(removeButton)
                }

                override fun mouseExited(e: MouseEvent) {
                    println("Exit")
                    if (panel.components.find { it.bounds.contains(e.point) } == null) {
                    //if (!removeButton.bounds.contains(e.point)) {
                        removeButton.isVisible = false
                        activeButtons.remove(removeButton)
                    }
                }
            })*/
            /*panel.addMouseMotionListener(object : MouseMotionListener {
                override fun mouseDragged(e: MouseEvent) {}
                override fun mouseMoved(e: MouseEvent) {
                    removeButton.isVisible = panel.contains(e.point)
                }
            })*/
        }

        box.add(glue)
    }

    private fun createItem(primName: String, args: List<Int>, result: Int, removed: () -> Unit): JPanel {
        val pauseIcon = FlatSVGIcon(javaClass.getResource("/close.svg"))
        pauseIcon.colorFilter = FlatSVGIcon.ColorFilter()
        val primaryColour = UIManager.getDefaults().getColor("Panel.foreground")
        pauseIcon.colorFilter.add(Color.black, primaryColour, primaryColour)
        val panel = JPanel()
        panel.border = EmptyBorder(10, 10, 10, 10)
        panel.layout = BorderLayout()
        val leftPanel = Box.createHorizontalBox()
        leftPanel.add(JLabel("<html>$primName(<b>${args.joinToString(", ")}</b>) = <b>$result</b><html/>"))
        //leftPanel.add(JTextField())
        panel.add(leftPanel, BorderLayout.WEST)
        panel.add(Box.createRigidArea(Dimension(16, 0)))
        val removeButton = JButton(pauseIcon)
        //removeButton.isBorderPainted = false
        removeButton.putClientProperty( "JButton.buttonType", "toolBarButton" );
        removeButton.addActionListener {
            box.remove(panel)
            box.revalidate()
            box.repaint()
            removed()
        }
        //removeButton.putClientProperty(FlatClientProperties.STYLE, "disabledBackground")
        panel.add(removeButton, BorderLayout.EAST)
        return panel
    }

    fun addItem(primName: String, args: List<Int>, result: Int, removed: () -> Unit) {
        box.remove(glue)
        box.add(createItem(primName, args, result, removed))
        box.add(glue)
        box.revalidate()
        box.repaint()
    }

    fun clear() {
        box.removeAll()
        box.add(glue)
        box.revalidate()
        box.repaint()
    }
}
