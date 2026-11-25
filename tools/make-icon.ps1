param(
    [string]$Root = (Split-Path -Parent $PSScriptRoot)
)

Add-Type -AssemblyName System.Drawing

function New-RoundedPath([float]$x, [float]$y, [float]$w, [float]$h, [float]$r) {
    $p = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $d = $r * 2
    if ($d -gt $w) { $d = $w }
    if ($d -gt $h) { $d = $h }
    $p.AddArc($x, $y, $d, $d, 180, 90)
    $p.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $p.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $p.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $p.CloseFigure()
    return $p
}

function New-IconBitmap([int]$size, [float]$content) {
    $bmp = [System.Drawing.Bitmap]::new($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)

    $s = $size / 1024.0
    $g.ScaleTransform($s, $s)
    $g.TranslateTransform(512, 512)
    $g.ScaleTransform($content, $content)
    $g.TranslateTransform(-512, -512)

    $state = $g.Save()
    $g.TranslateTransform(512, 512)
    $g.RotateTransform(-13.0)
    $g.TranslateTransform(-512, -512)

    $outer = New-RoundedPath 112 112 800 800 190
    $rect = [System.Drawing.Rectangle]::new(0, 0, 1024, 1024)
    $brush = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        $rect,
        [System.Drawing.Color]::FromArgb(255, 124, 92, 255),
        [System.Drawing.Color]::FromArgb(255, 58, 160, 255),
        45.0
    )
    $g.FillPath($brush, $outer)
    $brush.Dispose()

    $inner = New-RoundedPath 277 277 470 470 95
    $dark = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 10, 13, 18))
    $g.FillPath($dark, $inner)
    $dark.Dispose()
    $outer.Dispose()
    $inner.Dispose()
    $g.Restore($state)

    $fontFamily = $null
    foreach ($name in @("Segoe UI Black", "Arial Black", "Montserrat Black", "Verdana")) {
        try {
            $fontFamily = [System.Drawing.FontFamily]::new($name)
            break
        } catch {
            $fontFamily = $null
        }
    }
    if ($null -eq $fontFamily) {
        $fontFamily = [System.Drawing.FontFamily]::new("Arial")
    }
    $font = [System.Drawing.Font]::new($fontFamily, 430, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $fmt = [System.Drawing.StringFormat]::new()
    $fmt.Alignment = [System.Drawing.StringAlignment]::Center
    $fmt.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rectF = [System.Drawing.RectangleF]::new(0, 0, 1024, 1024)
    $white = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    $g.DrawString("S", $font, $white, $rectF, $fmt)
    $white.Dispose()
    $font.Dispose()
    $g.Dispose()
    return $bmp
}

function Save-Png($bmp, [string]$path) {
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

$densitySizes = [ordered]@{
    "mdpi"    = 48
    "hdpi"    = 72
    "xhdpi"   = 96
    "xxhdpi"  = 144
    "xxxhdpi" = 192
}

$foregroundSizes = [ordered]@{
    "mdpi"    = 108
    "hdpi"    = 162
    "xhdpi"   = 216
    "xxhdpi"  = 324
    "xxxhdpi" = 432
}

foreach ($key in $densitySizes.Keys) {
    $dir = Join-Path $Root "app\src\main\res\mipmap-$key"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $size = $densitySizes[$key]
    Save-Png (New-IconBitmap $size 0.96) (Join-Path $dir "ic_launcher.png")
    Save-Png (New-IconBitmap $size 0.96) (Join-Path $dir "ic_launcher_round.png")
}

foreach ($key in $foregroundSizes.Keys) {
    $dir = Join-Path $Root "app\src\main\res\mipmap-$key"
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $size = $foregroundSizes[$key]
    Save-Png (New-IconBitmap $size 0.70) (Join-Path $dir "ic_launcher_foreground.png")
}

Save-Png (New-IconBitmap 512 0.96) (Join-Path $Root "icon-512.png")

Write-Output "Icons written to $Root"
