param(
    [string]$OutputPath
)

$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$presentationDir = Join-Path $repoRoot 'presentation'
New-Item -ItemType Directory -Force -Path $presentationDir | Out-Null

if (-not $OutputPath) {
    $OutputPath = Join-Path $presentationDir 'ATdIT2_Space_Flight_15min_Presentation.pptx'
}

$OutputPath = [System.IO.Path]::GetFullPath($OutputPath)
$PdfPath = [System.IO.Path]::ChangeExtension($OutputPath, '.pdf')
$ScreenshotPath = Join-Path $repoRoot 'docs\prototype-dashboard.png'

$ppLayoutBlank = 12
$ppSaveAsOpenXmlPresentation = 24
$ppSaveAsPDF = 32
$msoFalse = 0
$msoTrue = -1
$msoShapeRectangle = 1
$msoShapeRoundedRectangle = 5
$msoTextOrientationHorizontal = 1
$ppAlignLeft = 1
$ppAlignCenter = 2

function Get-Rgb([int]$red, [int]$green, [int]$blue) {
    return $red + (256 * $green) + (65536 * $blue)
}

$colors = @{
    Navy = Get-Rgb 16 36 58
    Teal = Get-Rgb 19 141 166
    Orange = Get-Rgb 227 111 30
    Red = Get-Rgb 191 60 60
    Green = Get-Rgb 62 142 91
    Slate = Get-Rgb 70 88 109
    Light = Get-Rgb 246 248 251
    White = Get-Rgb 255 255 255
    Border = Get-Rgb 214 221 229
    SoftBlue = Get-Rgb 225 239 246
    SoftRed = Get-Rgb 248 228 226
    SoftGreen = Get-Rgb 227 242 230
    SoftOrange = Get-Rgb 252 235 221
    SoftTeal = Get-Rgb 224 244 247
}

function Set-SlideBackground($slide, [int]$rgb) {
    $slide.Background.Fill.Visible = $msoTrue
    $slide.Background.Fill.Solid()
    $slide.Background.Fill.ForeColor.RGB = $rgb
}

function Add-Box(
    $slide,
    [single]$left,
    [single]$top,
    [single]$width,
    [single]$height,
    [int]$fillColor,
    [int]$lineColor,
    [int]$shapeType = $msoShapeRoundedRectangle,
    [single]$lineWeight = 1.2
) {
    $shape = $slide.Shapes.AddShape($shapeType, $left, $top, $width, $height)
    $shape.Fill.Visible = $msoTrue
    $shape.Fill.Solid()
    $shape.Fill.ForeColor.RGB = $fillColor
    $shape.Line.Visible = $msoTrue
    $shape.Line.ForeColor.RGB = $lineColor
    $shape.Line.Weight = $lineWeight
    return $shape
}

function Add-TextBox(
    $slide,
    [string]$text,
    [single]$left,
    [single]$top,
    [single]$width,
    [single]$height,
    [int]$fontSize,
    [int]$fontColor,
    [string]$fontName = 'Aptos',
    [bool]$bold = $false,
    [int]$alignment = $ppAlignLeft
) {
    $shape = $slide.Shapes.AddTextbox($msoTextOrientationHorizontal, $left, $top, $width, $height)
    $shape.Line.Visible = $msoFalse
    $shape.Fill.Visible = $msoFalse
    $shape.TextFrame.WordWrap = $msoTrue
    $shape.TextFrame.AutoSize = 0
    $shape.TextFrame.MarginLeft = 0
    $shape.TextFrame.MarginRight = 0
    $shape.TextFrame.MarginTop = 0
    $shape.TextFrame.MarginBottom = 0
    $shape.TextFrame.TextRange.Text = $text
    $shape.TextFrame.TextRange.Font.Name = $fontName
    $shape.TextFrame.TextRange.Font.Size = $fontSize
    $shape.TextFrame.TextRange.Font.Bold = $(if ($bold) { $msoTrue } else { $msoFalse })
    $shape.TextFrame.TextRange.Font.Color.RGB = $fontColor
    $shape.TextFrame.TextRange.ParagraphFormat.Alignment = $alignment
    $shape.TextFrame.TextRange.ParagraphFormat.SpaceAfter = 6
    return $shape
}

function Add-Header($slide, [string]$partLabel, [int]$partColor, [string]$title, [string]$subtitle = '') {
    Add-Box $slide 0 0 960 14 $partColor $partColor $msoShapeRectangle 0 | Out-Null
    Add-Box $slide 42 24 130 28 $partColor $partColor $msoShapeRoundedRectangle 0 | Out-Null
    Add-TextBox $slide $partLabel 52 29 110 18 13 $colors.White 'Aptos' $true $ppAlignCenter | Out-Null
    Add-TextBox $slide $title 40 60 880 36 28 $colors.Navy 'Aptos Display' $true | Out-Null
    if ($subtitle) {
        Add-TextBox $slide $subtitle 40 98 880 22 14 $colors.Slate | Out-Null
    }
}

function Add-Card($slide, [single]$left, [single]$top, [single]$width, [single]$height, [int]$fillColor, [string]$title, [string]$body) {
    Add-Box $slide $left $top $width $height $fillColor $colors.Border $msoShapeRoundedRectangle 1.2 | Out-Null
    Add-TextBox $slide $title ($left + 14) ($top + 12) ($width - 28) 26 18 $colors.Navy 'Aptos' $true | Out-Null
    Add-TextBox $slide $body ($left + 14) ($top + 44) ($width - 28) ($height - 56) 14 $colors.Slate | Out-Null
}

function Add-StepBox($slide, [single]$left, [single]$top, [single]$width, [single]$height, [int]$fillColor, [string]$text) {
    Add-Box $slide $left $top $width $height $fillColor $colors.Border $msoShapeRoundedRectangle 1.1 | Out-Null
    Add-TextBox $slide $text ($left + 8) ($top + 14) ($width - 16) ($height - 20) 13 $colors.Navy 'Aptos' $true $ppAlignCenter | Out-Null
}

function Add-ArrowText($slide, [string]$text, [single]$left, [single]$top, [int]$fontColor) {
    Add-TextBox $slide $text $left $top 25 20 20 $fontColor 'Aptos' $true $ppAlignCenter | Out-Null
}

function Add-Callout($slide, [single]$left, [single]$top, [single]$width, [single]$height, [int]$lineColor, [string]$label, [single]$labelLeft, [single]$labelTop) {
    $shape = Add-Box $slide $left $top $width $height $colors.White $lineColor $msoShapeRoundedRectangle 2.0
    $shape.Fill.Transparency = 1.0
    Add-Box $slide $labelLeft $labelTop 170 28 $lineColor $lineColor $msoShapeRoundedRectangle 0 | Out-Null
    Add-TextBox $slide $label ($labelLeft + 8) ($labelTop + 6) 154 16 12 $colors.White 'Aptos' $true $ppAlignCenter | Out-Null
}

if (Test-Path $OutputPath) {
    Remove-Item $OutputPath -Force
}

if (Test-Path $PdfPath) {
    Remove-Item $PdfPath -Force
}

$powerPoint = $null
$presentation = $null

try {
    $powerPoint = New-Object -ComObject PowerPoint.Application
    $presentation = $powerPoint.Presentations.Add()
    $presentation.PageSetup.SlideSize = 15

    # Slide 1
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Navy
    Add-Box $slide 0 0 960 18 $colors.Teal $colors.Teal $msoShapeRectangle 0 | Out-Null
    Add-TextBox $slide 'Space Flight Phase' 46 72 430 40 30 $colors.White 'Aptos Display' $true | Out-Null
    Add-TextBox $slide 'In-Flight Passenger Support & Emergency Escalation System' 46 118 560 54 24 $colors.White 'Aptos' $true | Out-Null
    Add-TextBox $slide '15-minute presentation | fictional company context: space tourism provider run by "Elon Bezos"' 46 178 590 26 14 $colors.SoftTeal | Out-Null
    Add-Card $slide 46 250 275 158 $colors.SoftBlue 'Presentation goal' "Show the business process in the Space Flight phase, explain the software prototype focus, and identify the next iteration."
    Add-Card $slide 342 250 275 158 $colors.SoftGreen 'Core promise' "The process must work in the normal support case and also when incidents become critical or onboard support is unavailable."
    Add-Card $slide 638 250 276 158 $colors.SoftOrange 'Team' "Wenhuan Liang`r`nVivienne Wuehl`r`nValentin Reifke`r`nLouis Burckel`r`nTim Vetter"
    Add-TextBox $slide 'Status: prototype and presentation-ready process thinking after the second project session' 46 450 868 22 14 $colors.White | Out-Null

    # Slide 2
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'TODAY' $colors.Teal 'Storyline for the next 15 minutes' 'The deck is structured around the three parts your professor explicitly asked for.'
    Add-Card $slide 44 150 270 220 $colors.SoftBlue '1. Present business process and main personas' "What happens during the flight, which actors are active, and how normal and exceptional cases differ."
    Add-Card $slide 345 150 270 220 $colors.SoftOrange '2. Explain the focus of the software prototype' "Why the prototype is intentionally narrow, what the JavaFX application supports, and how it addresses the professor feedback."
    Add-Card $slide 646 150 270 220 $colors.SoftGreen '3. Open points for the next iteration' "What is still group work, where BPMN in Signavio needs refinement, and which new personas or edge cases should be added."
    Add-TextBox $slide 'Recommended pacing: 5 min process, 6 min prototype, 3 min next iteration, 1 min questions.' 44 410 870 20 14 $colors.Slate | Out-Null

    # Slide 3
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 1' $colors.Teal 'Business problem and project scope' 'We are responsible only for the Space Flight phase, not the whole enterprise platform.'
    Add-Card $slide 44 150 410 240 $colors.SoftRed 'Business problem in the flight phase' "• Passengers can experience stress, nausea, panic, or medical symptoms during the actual flight.`r`n• Without structured software support, reactions are ad hoc and difficult to trace.`r`n• The professor explicitly challenged us to think beyond the happy path.`r`n• A critical point is that onboard support itself can become unavailable."
    Add-Card $slide 500 150 414 240 $colors.SoftBlue 'Chosen scope of this team' "• Focused core process: in-flight passenger support and emergency escalation.`r`n• Two required cases: normal support case and emergency or fallback case.`r`n• Priorities: clarity, maintainability, presentation value, and realistic proof-of-concept scope.`r`n• Out of scope: real telemetry, real AI diagnosis, enterprise platform features, and advanced integration."
    Add-TextBox $slide 'Key framing sentence for the presentation: We do not solve all company problems; we solve one high-risk process in the actual flight phase.' 44 430 870 26 15 $colors.Navy 'Aptos' $true | Out-Null

    # Slide 4
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 1' $colors.Teal 'Business process overview' 'The process has one normal path and one escalation path, both visible in the prototype.'
    Add-TextBox $slide 'Normal support case' 44 138 250 24 18 $colors.Green 'Aptos' $true | Out-Null
    Add-TextBox $slide 'Emergency or fallback case' 44 282 280 24 18 $colors.Red 'Aptos' $true | Out-Null

    $normalY = 176
    Add-StepBox $slide 44 $normalY 150 58 $colors.SoftBlue 'Passenger reports discomfort'
    Add-ArrowText $slide '>' 199 ($normalY + 16) $colors.Slate
    Add-StepBox $slide 226 $normalY 150 58 $colors.SoftBlue 'Onboard support assesses'
    Add-ArrowText $slide '>' 381 ($normalY + 16) $colors.Slate
    Add-StepBox $slide 408 $normalY 150 58 $colors.SoftBlue 'Support action selected'
    Add-ArrowText $slide '>' 563 ($normalY + 16) $colors.Slate
    Add-StepBox $slide 590 $normalY 150 58 $colors.SoftBlue 'Passenger monitored'
    Add-ArrowText $slide '>' 745 ($normalY + 16) $colors.Slate
    Add-StepBox $slide 772 $normalY 142 58 $colors.SoftGreen 'Incident resolved'

    $emergencyY = 320
    Add-StepBox $slide 44 $emergencyY 150 58 $colors.SoftOrange 'Critical symptom or support unavailable'
    Add-ArrowText $slide '>' 199 ($emergencyY + 16) $colors.Slate
    Add-StepBox $slide 226 $emergencyY 150 58 $colors.SoftOrange 'System escalates automatically'
    Add-ArrowText $slide '>' 381 ($emergencyY + 16) $colors.Slate
    Add-StepBox $slide 408 $emergencyY 150 58 $colors.SoftOrange 'Base station takes over'
    Add-ArrowText $slide '>' 563 ($emergencyY + 16) $colors.Slate
    Add-StepBox $slide 590 $emergencyY 150 58 $colors.SoftOrange 'Emergency response coordinated'
    Add-ArrowText $slide '>' 745 ($emergencyY + 16) $colors.Slate
    Add-StepBox $slide 772 $emergencyY 142 58 $colors.SoftRed 'Traceable closure'

    Add-Card $slide 44 416 870 74 $colors.White 'Workflow states represented in software' 'New -> Assessing -> Monitoring -> Escalated -> Resolved'

    # Slide 5
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 1' $colors.Teal 'Main personas in the core process' 'These are the active user groups that should also be visible in the BPMN model.'
    Add-Card $slide 44 150 270 250 $colors.SoftBlue 'Passenger' "Examples: Jennifer Monroe and Ben Cooper.`r`n`r`n• reports discomfort or receives support`r`n• can be anxious, panicked, or medically affected`r`n• needs clear communication and reassurance"
    Add-Card $slide 345 150 270 250 $colors.SoftOrange 'Onboard Support' "Example: Emma Bright.`r`n`r`n• first line of support during the flight`r`n• assesses normal incidents`r`n• chooses support action and monitors the passenger"
    Add-Card $slide 646 150 270 250 $colors.SoftGreen 'Base Station Operator' "Example: Brendon Fitz.`r`n`r`n• remote backup and supervisory role`r`n• takes over escalated or fallback cases`r`n• needs full visibility and traceability"
    Add-TextBox $slide 'Important argument in the presentation: the personas directly drive the process, the software behavior, and the handover logic.' 44 430 870 24 15 $colors.Navy 'Aptos' $true | Out-Null

    # Slide 6
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'BPMN / PERSONAS' $colors.Orange 'Suggestions for Signavio and for the next persona set' 'This reflects the team discussion about additional expert roles and more realistic passenger diversity.'
    Add-Card $slide 44 150 420 270 $colors.SoftBlue 'BPMN suggestions for Signavio' "Recommended lanes:`r`n• Passenger`r`n• Onboard Support`r`n• Base Station Operator`r`n• Support System`r`n`r`nUseful gateways:`r`n• Is severity critical?`r`n• Is onboard support available?`r`n• Is the passenger responsive?`r`n`r`nUseful data objects:`r`n• incident record`r`n• severity level`r`n• action history`r`n• passenger condition information"
    Add-Card $slide 500 150 414 270 $colors.SoftOrange 'Persona ideas discussed in the group' "Possible next personas:`r`n• Remote medical doctor at the base station for medical emergencies`r`n• Remote psychologist or calming specialist for stress and panic`r`n• Teen passenger or accompanied minor for age-specific communication and responsibility`r`n`r`nModeling tip: only add a separate BPMN lane if that role actively performs tasks or exchanges messages."
    Add-TextBox $slide 'Presentation wording suggestion: these expert personas are good next-iteration extensions because they deepen the process without changing our current prototype scope.' 44 446 870 34 14 $colors.Slate | Out-Null

    # Slide 7
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 2' $colors.Orange 'Focus of the software prototype' 'The prototype is intentionally small but directly supports the most critical process steps.'
    Add-Card $slide 44 150 278 280 $colors.SoftBlue 'Implemented in the prototype' "• JavaFX desktop application`r`n• passenger overview`r`n• incident creation`r`n• severity classification`r`n• drag-and-drop status board`r`n• action history logging"
    Add-Card $slide 341 150 278 280 $colors.SoftGreen 'Exception handling that matters' "• automatic escalation of CRITICAL incidents`r`n• fallback escalation when onboard support is unavailable`r`n• base station takeover`r`n• clear status transitions`r`n• traceability for every important action"
    Add-Card $slide 638 150 278 280 $colors.SoftOrange 'Mocked on purpose' "• real telemetry and spacecraft systems`r`n• AI diagnosis`r`n• network communication`r`n• database persistence`r`n• advanced analytics"
    Add-TextBox $slide 'Design claim: we chose a realistic proof of concept, not a fake enterprise system.' 44 448 870 24 15 $colors.Navy 'Aptos' $true | Out-Null

    # Slide 8
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 2' $colors.Orange 'Prototype screenshot: current JavaFX dashboard' 'One screen already shows the core process, both main scenarios, and the exception-oriented workflow.'
    if (Test-Path $ScreenshotPath) {
        $picture = $slide.Shapes.AddPicture($ScreenshotPath, $msoFalse, $msoTrue, 55, 130, 850, 360)
        $picture.Line.Visible = $msoTrue
        $picture.Line.ForeColor.RGB = $colors.Border
        Add-Callout $slide 62 145 220 330 $colors.Teal 'Passenger overview + incident creation' 62 107
        Add-Callout $slide 292 145 350 330 $colors.Orange 'Drag-and-drop workflow board' 388 107
        Add-Callout $slide 650 145 245 330 $colors.Green 'Incident details + action history' 646 107
        Add-Callout $slide 620 68 285 38 $colors.Red 'Availability toggle supports fallback case' 676 72
    }
    else {
        Add-Card $slide 120 180 720 220 $colors.White 'Screenshot missing' 'The script expected docs/prototype-dashboard.png. Capture that image first, then rerun the presentation generator.'
    }

    # Slide 9
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'SCENARIO 1' $colors.Green 'Normal support case in the prototype' 'This is the demo flow for mild anxiety, nausea, or stress.'
    Add-Card $slide 44 150 470 280 $colors.SoftBlue 'Step-by-step flow' "1. Passenger reports discomfort.`r`n2. Incident appears in New.`r`n3. Onboard support moves it to Assessing.`r`n4. Support action is selected and logged.`r`n5. Incident moves to Monitoring.`r`n6. When stable, the case is resolved."
    Add-Card $slide 540 150 374 280 $colors.SoftGreen 'Why this matters' "• The workflow is visible instead of ad hoc.`r`n• Actions are documented instead of only verbal.`r`n• The board gives structure to the handling process.`r`n• The user can explain this clearly during the demo."
    Add-TextBox $slide 'Good transition sentence for speaking: the normal case demonstrates service quality, but the emergency path demonstrates process resilience.' 44 448 870 24 14 $colors.Slate | Out-Null

    # Slide 10
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'SCENARIO 2' $colors.Red 'Emergency or fallback case in the prototype' 'This slide is the answer to the professor feedback about exception handling and support unavailability.'
    Add-Card $slide 44 150 470 280 $colors.SoftOrange 'Trigger and response' "Trigger A: incident severity is CRITICAL.`r`nTrigger B: onboard support becomes unavailable.`r`n`r`nSystem response:`r`n• status changes to Escalated`r`n• responsible role changes to Base Station`r`n• history log records the reason`r`n• case remains visible in the same board"
    Add-Card $slide 540 150 374 280 $colors.SoftRed 'Why this is presentation-relevant' "• It proves we did not design only a happy path.`r`n• It answers the question: what happens if support is incapacitated?`r`n• It gives the prototype a clear differentiating point.`r`n• It creates a stronger BPMN model with exception gateways."
    Add-TextBox $slide 'Key line to say: the software still supports the process even when the onboard support role cannot continue normally.' 44 448 870 24 15 $colors.Navy 'Aptos' $true | Out-Null

    # Slide 11
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 2' $colors.Orange 'How the prototype fits the professor requirements' 'This is the bridge between the code and the grading criteria.'
    Add-Card $slide 44 150 270 260 $colors.SoftBlue 'Process thinking' "• clear scope in the Space Flight phase`r`n• personas tied to the core process`r`n• normal case and emergency case`r`n• BPMN-ready process descriptions"
    Add-Card $slide 345 150 270 260 $colors.SoftGreen 'Software engineering' "• layered architecture`r`n• UI separated from business logic`r`n• enums for status and severity`r`n• SOLID, DRY, KISS, YAGNI"
    Add-Card $slide 646 150 270 260 $colors.SoftOrange 'Demo readiness' "• runnable JavaFX application`r`n• drag-and-drop workflow board`r`n• action history and traceability`r`n• JUnit tests for key rules"
    Add-TextBox $slide 'Good conclusion for this slide: the prototype is small, but it is defensible from both a process and a software-engineering perspective.' 44 438 870 26 14 $colors.Slate | Out-Null

    # Slide 12
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Light
    Add-Header $slide 'PART 3' $colors.Teal 'Open points for the next iteration (group work)' 'This is where you show realism: what is already done, and what is still intentionally open.'
    Add-Card $slide 44 150 205 200 $colors.SoftBlue 'BPMN in Signavio' "Finalize lanes, gateways, exception paths, and data objects for both cases."
    Add-Card $slide 267 150 205 200 $colors.SoftOrange 'Additional personas' "Decide whether remote doctor, psychologist, and teen passenger become active process roles."
    Add-Card $slide 490 150 205 200 $colors.SoftGreen 'Prototype refinement' "Role-specific views, better demo flow, richer validation, and more realistic escalation communication."
    Add-Card $slide 713 150 205 200 $colors.SoftRed 'Technical depth' "Persistence, telemetry simulation, notifications, and stronger reporting if time allows."
    Add-Card $slide 44 380 874 86 $colors.White 'Group recommendation' 'Keep the next iteration focused: add one new expert role and one new exception case rather than expanding everything at once.'

    # Slide 13
    $slide = $presentation.Slides.Add($presentation.Slides.Count + 1, $ppLayoutBlank)
    Set-SlideBackground $slide $colors.Navy
    Add-Box $slide 0 0 960 18 $colors.Orange $colors.Orange $msoShapeRectangle 0 | Out-Null
    Add-TextBox $slide 'Takeaway' 46 88 300 36 30 $colors.White 'Aptos Display' $true | Out-Null
    Add-Card $slide 46 160 868 226 $colors.White 'Final message for the professor' "• We support one clearly scoped business process in the Space Flight phase.`r`n• We show the main personas and the handover between them.`r`n• We built a runnable prototype for both the normal support case and the emergency or fallback case.`r`n• We know exactly what belongs to the next iteration: BPMN detail, extra expert roles, and deeper realism."
    Add-TextBox $slide 'Questions?' 46 428 200 32 22 $colors.White 'Aptos' $true | Out-Null
    Add-TextBox $slide 'Backup note: if asked about future work, point to Signavio BPMN completion, remote doctor / psychologist roles, and richer telemetry integration.' 46 466 868 26 14 $colors.SoftTeal | Out-Null

    $presentation.SaveAs($OutputPath, $ppSaveAsOpenXmlPresentation)
    $presentation.SaveAs($PdfPath, $ppSaveAsPDF)
}
finally {
    if ($presentation) {
        $presentation.Close()
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($presentation)
    }
    if ($powerPoint) {
        $powerPoint.Quit()
        [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($powerPoint)
    }
    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}

Write-Output $OutputPath
Write-Output $PdfPath
