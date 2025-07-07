# Скрипт для перемещения файлов platform SDK
$sourcePath = "D:\Android\Sdk\platforms\android-35\android-35"
$destPath = "D:\Android\Sdk\platforms\android-35"

# Перемещаем все файлы и папки
Get-ChildItem -Path $sourcePath | Move-Item -Destination $destPath -Force

# Удаляем пустую вложенную папку
Remove-Item -Path $sourcePath -Force

Write-Host "Platform SDK files moved successfully!" 