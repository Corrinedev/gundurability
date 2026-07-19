This mod provides data-driven stat modifiers for guns in the TACZ mod. All modifier behavior is defined through JSON files, allowing gun attributes to be adjusted without writing code.

The system supports modifying values such as Damage, Recoil, RPM, ADS, AmmoSpeed, Inaccuracy. Modifiers can apply thresholds, reduction limits, and multiple affected stats within a single definition, either to a category or indivdual GunId.

This addon is intended for modpack makers and players who want to add vanilla-style durability to TACZ weapons through datapacks, using an easy and configurable format.

KubeJS is recommended for easy modifying of the datapack globally, you can also create your own datapack entries with your own namespaces, such as "my_datapack/gundb/".

Datapack Examples

You can also disable certain gunid's or categories easily. The gunid overwrites the category so even if the Heavy category is enabled, this minigun would not have durability. "enabled" is an optional field, and is true by default.

