#!/bin/bash
# Script pour régénérer la configuration runClient2 correctement
# Utile après un changement de PC ou une réinitialisation du projet

set -e

echo "🔄 Régénération de la configuration runClient2..."

# Nettoie les anciens artefacts
rm -f .idea/runConfigurations/runClient2.xml

# Génère les configurations
./gradlew genIntelliJRuns fixRunClient2Config

echo ""
echo "✅ runClient2 a été régénérée avec succès !"
echo "📝 N'oublie pas de recharger IntelliJ (File → Invalidate Caches → Invalidate and Restart)"

