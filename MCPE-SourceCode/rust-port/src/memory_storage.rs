use std::collections::HashMap;

#[derive(Default)]
pub struct MemoryChunkStorage {
    chunks: HashMap<(i32, i32), Vec<u8>>,
}

impl MemoryChunkStorage {
    pub fn save_chunk(&mut self, x: i32, z: i32, data: Vec<u8>) {
        self.chunks.insert((x, z), data);
    }

    pub fn load_chunk(&self, x: i32, z: i32) -> Option<&[u8]> {
        self.chunks.get(&(x, z)).map(|v| v.as_slice())
    }
}

#[derive(Default)]
pub struct MemoryLevelStorage {
    storage: MemoryChunkStorage,
}

impl MemoryLevelStorage {
    pub fn create_chunk_storage(&mut self) -> &mut MemoryChunkStorage {
        &mut self.storage
    }
}

pub struct MemoryLevelStorageSource;

impl MemoryLevelStorageSource {
    pub fn new() -> Self {
        Self
    }

    pub fn get_name(&self) -> &'static str {
        "Memory Storage"
    }

    pub fn select_level(&self, _level_id: &str) -> MemoryLevelStorage {
        MemoryLevelStorage::default()
    }

    pub fn is_new_level_id_acceptable(&self, _level_id: &str) -> bool {
        true
    }
}

