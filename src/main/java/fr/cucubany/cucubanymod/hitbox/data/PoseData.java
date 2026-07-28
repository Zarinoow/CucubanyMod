package fr.cucubany.cucubanymod.hitbox.data;

import fr.cucubany.cucubanymod.hitbox.BodyPart;
import fr.cucubany.cucubanymod.hitbox.PartTransform;

import java.util.EnumMap;
import java.util.Map;

public class PoseData {
    private final Map<BodyPart, PartTransformData[]> transforms;

    public PoseData(Map<BodyPart, PartTransformData[]> transforms) {
        this.transforms = transforms;
    }

    public PartTransform[] getTransforms(double baseX, double baseY, double baseZ, float yBodyRot, BodyPart part) {
        PartTransformData[] data = transforms.get(part);
        if (data == null || data.length == 0) return new PartTransform[0];

        float rad = yBodyRot * ((float) Math.PI / 180F);
        double lx = Math.cos(rad);
        double lz = Math.sin(rad);
        double bx = Math.sin(rad);
        double bz = -Math.cos(rad);

        PartTransform[] result = new PartTransform[data.length];
        for (int i = 0; i < data.length; i++) {
            PartTransformData d = data[i];
            double wx = baseX + lx * d.lateralOffset + bx * d.backwardOffset;
            double wy = baseY + d.yOffset;
            double wz = baseZ + lz * d.lateralOffset + bz * d.backwardOffset;
            result[i] = PartTransform.of(wx, wy, wz, d.width, d.height);
        }
        return result;
    }

    public PartTransformData[] get(BodyPart part) {
        return transforms.get(part);
    }

    public PoseData deepCopy() {
        Map<BodyPart, PartTransformData[]> copy = new EnumMap<>(BodyPart.class);
        for (Map.Entry<BodyPart, PartTransformData[]> entry : transforms.entrySet()) {
            PartTransformData[] arr = entry.getValue();
            PartTransformData[] arrCopy = new PartTransformData[arr.length];
            for (int i = 0; i < arr.length; i++) {
                arrCopy[i] = arr[i].deepCopy();
            }
            copy.put(entry.getKey(), arrCopy);
        }
        return new PoseData(copy);
    }
}
