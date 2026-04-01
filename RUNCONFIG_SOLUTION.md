# Solution: Configuration runClient2 pour CucubanyMod

## Problème
ForgeGradle (version 5.1) ne génère pas complètement la configuration `runClient2` dans IntelliJ :
- La classe principale était vide
- Les paramètres VM et de programme manquaient
- Le fichier de classpath n'était pas créé

## Solution implémentée

### 1. Configuration du build.gradle

J'ai ajouté trois éléments clés au `build.gradle` :

#### a) Tâche `fixRunClient2Config`
```gradle
tasks.register('fixRunClient2Config') {
    // Copie les paramètres du runClient vers runClient2
    // Adapte le chemin du classpath (runClient2_minecraftClasspath.txt)
    // Ajoute les variables d'environnement manquantes (MCP_MAPPINGS)
}
```

#### b) Tâche `ensureRunClient2Classpath`
```gradle
tasks.register('ensureRunClient2Classpath') {
    // Copie le fichier de classpath de runClient vers runClient2
    // Assurant que runClient2_minecraftClasspath.txt existe
}
```

#### c) Configuration afterEvaluate
```gradle
afterEvaluate {
    if (tasks.findByName('prepareRunClient2')) {
        tasks.prepareRunClient2.dependsOn 'ensureRunClient2Classpath'
    }
}
```

### 2. Script de régénération

Un script `regenerateRunClient2.sh` a été créé pour faciliter la régénération sur un nouveau PC.

**Utilisation :**
```bash
./regenerateRunClient2.sh
```

Ou manuellement :
```bash
./gradlew genIntelliJRuns fixRunClient2Config
```

## Procédure d'utilisation

### Première fois (ou après avoir changé de PC)

1. **Exécute le script de régénération :**
   ```bash
   ./regenerateRunClient2.sh
   ```

2. **Recharge IntelliJ :**
   - File → Invalidate Caches → Invalidate and Restart

3. **Vérifies que la classe principale est définie :**
   - Run → Edit Configurations
   - Cherche `runClient2`
   - Vérifie que "Main class" = `cpw.mods.bootstraplauncher.BootstrapLauncher`

### Après chaque modification du build.gradle

Si tu modifies la configuration `client2` dans le `build.gradle`, regénère simplement :
```bash
./gradlew genIntelliJRuns fixRunClient2Config
```

## Détails techniques

### Pourquoi ForgeGradle n'a pas généré runClient2 correctement ?

ForgeGradle 5.1 traite spécialement les configurations nommées `client` et `server`. Les configurations supplémentaires (comme `client2`) sont :
- Reconnues mais ne reçoivent pas les mêmes paramètres
- N'ont pas de classe principale définie
- N'ont pas de classpath généré

### Comment la solution fonctionne

1. **fixRunClient2Config** : Modifie le XML généré par ForgeGradle pour copier les paramètres de `client`
2. **ensureRunClient2Classpath** : Crée le fichier de classpath en le copiant de `runClient`
3. **Dépendances** : `prepareRunClient2` dépend maintenant de `ensureRunClient2Classpath`, assurant que le classpath existe avant le lancement

### Portabilité entre PCs

La solution est **100% portable** car :
- ✅ Basée sur des tâches Gradle (exécutées dans n'importe quel environnement)
- ✅ Adapte les chemins absolus en utilisant des relatives (`.idea`, `build`, etc.)
- ✅ Ne dépend pas de configurations manuelles éditoriales
- ✅ Régénérable avec une seule commande

## Commandes utiles

```bash
# Régénérer toutes les configurations
./gradlew genIntelliJRuns fixRunClient2Config

# Ou utiliser le script
./regenerateRunClient2.sh

# Préparer runClient2 pour l'exécution
./gradlew prepareRunClient2

# Vérifier que tout est en place
ls -la build/classpath/runClient*_minecraftClasspath.txt
```

## Notes

- Le nom d'utilisateur pour `runClient2` est configuré comme `Dev2` dans le `build.gradle`
- Tu peux changer ce nom en modifiant : `args '--username', 'Dev2'` dans la section `client2`
- La configuration `runClient2` utilise le même répertoire de travail que `runClient` (peut être changé en modifiant `workingDirectory project.file('run/client2')`)

